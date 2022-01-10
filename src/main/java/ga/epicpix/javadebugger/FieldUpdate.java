package ga.epicpix.javadebugger;

import ga.epicpix.javadebugger.typeid.TypeId;

public record FieldUpdate(TypeId fieldId, UntaggedValue val) {}
