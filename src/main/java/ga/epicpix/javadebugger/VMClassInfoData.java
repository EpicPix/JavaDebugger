package ga.epicpix.javadebugger;

import ga.epicpix.javadebugger.typeid.TypeId;

public record VMClassInfoData(RefType refTypeTag, TypeId referenceTypeId, String signature, ClassLoadStatus status) {}
