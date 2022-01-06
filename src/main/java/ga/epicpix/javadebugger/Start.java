package ga.epicpix.javadebugger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import ga.epicpix.javadebugger.typeid.TypeId;
import ga.epicpix.javadebugger.typeid.TypeIdArgumentType;
import ga.epicpix.javadebugger.typeid.TypeIdTypes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Start {

    public static LiteralArgumentBuilder<Debugger> literal(String literal) {
        return LiteralArgumentBuilder.literal(literal);
    }

    public static <S> RequiredArgumentBuilder<Debugger, S> argument(String name, ArgumentType<S> arg) {
        return RequiredArgumentBuilder.argument(name, arg);
    }

    public interface ThrowRunnable {
        void run(Debugger debugger) throws Exception;
    }

    public static int silenceException(CommandContext<Debugger> debugger, ThrowRunnable runnable) throws CommandSyntaxException {
        try {
            runnable.run(debugger.getSource());
            return 1;
        } catch(CommandSyntaxException e) {
            throw e;
        } catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void alias(CommandDispatcher<Debugger> dispatcher, LiteralCommandNode<Debugger> command, String... aliases) {
        for(String alias : aliases)
            dispatcher.register(literal(alias).redirect(command).executes(command.getCommand()));
    }

    public static void registerCommands(CommandDispatcher<Debugger> dispatcher, Debugger deb) throws IOException {
        alias(dispatcher, dispatcher.register(literal("capabilities").executes(d -> silenceException(d, (debugger) -> {
            System.out.println("Capabilities:");
            debugger.Capabilities().Print();
        }))), "caps");

        alias(dispatcher, dispatcher.register(literal("version").executes(d -> silenceException(d, (debugger) -> {
            debugger.Version().Print();
        }))), "ver");

        alias(dispatcher, dispatcher.register(literal("quit").executes(d -> silenceException(d, (debugger) -> {
            System.exit(0);
        }))), "q");

        dispatcher.register(literal("idsizes").executes(d -> silenceException(d, (debugger) -> {
            System.out.println("Id Sizes:");
            debugger.IdSizes().Print();
        })));

        dispatcher.register(literal("allclasses").executes(d -> silenceException(d, (debugger) -> {
            System.out.println("All Loaded Classes:");
            ArrayList<VMClassInfoData> classList = debugger.AllClasses();
            int maxStatusLength = "[VERIFIED, PREPARED, INITIALIZED]".length();
            int maxRefTypeLength = ReferenceType.INTERFACE.name().length();
            for(VMClassInfoData classInfo : classList) {
                String status = "[" + classInfo.status().getStatus() + "]";
                System.out.println(classInfo.referenceTypeId() + " - " + status + " ".repeat(maxStatusLength - status.length()) + " - " + classInfo.refTypeTag() + " ".repeat(maxRefTypeLength - classInfo.refTypeTag().name().length()) + " " + classInfo.signature());
            }
        })));

        dispatcher.register(literal("allthreads").executes(d -> silenceException(d, (debugger) -> {
            System.out.println("All Threads:");
            ArrayList<TypeId> threadIds = debugger.AllThreads();
            for(TypeId threadId : threadIds) {
                System.out.println(threadId + " - \"" + debugger.ThreadName(threadId) + "\"");
            }
        })));

        dispatcher.register(literal("createstring").then(argument("string", StringArgumentType.string()).executes(d -> silenceException(d, (debugger) -> {
            System.out.println("String Id: " + debugger.CreateString(StringArgumentType.getString(d, "string")));
        }))));

        dispatcher.register(literal("methods").then(argument("typeid", TypeIdArgumentType.typeId(deb.IdSizes(), TypeIdTypes.OBJECT_ID)).executes(d -> silenceException(d, (debugger) -> {
            System.out.println("Methods:");
            ArrayList<VMMethodInfoData> methodList = debugger.Methods(d.getArgument("typeid", TypeId.class));
            for(VMMethodInfoData methodInfo : methodList) {
                System.out.println(methodInfo.methodId() + " " + methodInfo.name() + " " + methodInfo.signature() + " " + methodInfo.modBits());
            }
        }))));

        dispatcher.register(literal("kill").executes(d -> silenceException(d, (debugger) -> {
            debugger.Exit(0);
            System.exit(0);
        })).then(argument("exitCode", IntegerArgumentType.integer(0)).executes(d -> silenceException(d, (debugger) -> {
            debugger.Exit(IntegerArgumentType.getInteger(d, "exitCode"));
            System.exit(0);
        }))));
    }

    public static void main(String[] args) throws IOException {
        if(args.length >= 1) {
            String host = args[0];

            String address = host.split(":")[0];
            int port = Integer.parseInt(host.split(":", 2)[1]);

            Socket socket = new Socket(address, port);
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            DataInputStream input = new DataInputStream(socket.getInputStream());
            Debugger debugger = new Debugger(output, input);

            if(debugger.PerformHandshake()) {
                System.out.println("Handshake succeeded");
                Scanner scanner = new Scanner(System.in);

                CommandDispatcher<Debugger> dispatcher = new CommandDispatcher<>();
                registerCommands(dispatcher, debugger);
                while(true) {
                    if(scanner.hasNextLine()) {
                        String line = scanner.nextLine().replace("\\n", "\n");
                        try {
                            dispatcher.execute(line, debugger);
                        } catch (CommandSyntaxException e) {
                            System.err.println(e.getMessage());
                        }
                    }
                }
            }else throw new RuntimeException("Handshake failed");
        }else {
            throw new IllegalArgumentException("Missing required argument 'host'");
        }
    }

}
