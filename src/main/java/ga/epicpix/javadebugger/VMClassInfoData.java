package ga.epicpix.javadebugger;

import ga.epicpix.javadebugger.typeid.TypeId;

public record VMClassInfoData(ReferenceType refTypeTag, TypeId referenceTypeId, String signature, ClassLoadStatus status) {}
