package ga.epicpix.javadebugger;

import ga.epicpix.javadebugger.typeid.TypeId;

public record VMMethodInfoData(TypeId methodId, String name, String signature, int modBits) {}
