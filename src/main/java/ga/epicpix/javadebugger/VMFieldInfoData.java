package ga.epicpix.javadebugger;

import ga.epicpix.javadebugger.typeid.TypeId;

public record VMFieldInfoData(TypeId fieldId, String name, String signature, int modBits) {}
