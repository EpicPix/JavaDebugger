package ga.epicpix.javadebugger;

// https://docs.oracle.com/javase/8/docs/platform/jpda/jdwp/jdwp-protocol.html#JDWP_Error
public enum ErrorCodes {

    NONE(0, "No error has occurred."),
    INVALID_THREAD(10, "Passed thread is null, is not a valid thread or has exited."),
    INVALID_THREAD_GROUP(11, "Thread group invalid."),
    INVALID_PRIORITY(12, "Invalid priority."),
    THREAD_NOT_SUSPENDED(13, "If the specified thread has not been suspended by an event."),
    THREAD_SUSPENDED(14, "Thread already suspended."),
    THREAD_NOT_ALIVE(15, "Thread has not been started or is now dead."),
    INVALID_OBJECT(20, "If this reference type has been unloaded and garbage collected."),
    INVALID_CLASS(21, "Invalid class."),
    CLASS_NOT_PREPARED(22, "Class has been loaded but not yet prepared."),
    INVALID_METHODID(23, "Invalid method."),
    INVALID_LOCATION(24, "Invalid location."),
    INVALID_FIELDID(25, "Invalid field."),
    INVALID_FRAMEID(30, "Invalid jframeID."),
    NO_MORE_FRAMES(31, "There are no more Java or JNI frames on the call stack."),
    OPAQUE_FRAME(32, "Information about the frame is not available."),
    NOT_CURRENT_FRAME(33, "Operation can only be performed on current frame."),
    TYPE_MISMATCH(34, "The variable is not an appropriate type for the function used."),
    INVALID_SLOT(35, "Invalid slot."),
    DUPLICATE(40, "Item already set."),
    NOT_FOUND(41, "Desired element not found."),
    INVALID_MONITOR(50, "Invalid monitor."),
    NOT_MONITOR_OWNER(51, "This thread doesn't own the monitor."),
    INTERRUPT(52, "The call has been interrupted before completion."),
    INVALID_CLASS_FORMAT(60, "The virtual machine attempted to read a class file and determined that the file is malformed or otherwise cannot be interpreted as a class file."),
    CIRCULAR_CLASS_DEFINITION(61, "A circularity has been detected while initializing a class."),
    FAILS_VERIFICATION(62, "The verifier detected that a class file, though well formed, contained some sort of internal inconsistency or security problem."),
    ADD_METHOD_NOT_IMPLEMENTED(63, "Adding methods has not been implemented."),
    SCHEMA_CHANGE_NOT_IMPLEMENTED(64, "Schema change has not been implemented."),
    INVALID_TYPESTATE(65, "The state of the thread has been modified, and is now inconsistent."),
    HIERARCHY_CHANGE_NOT_IMPLEMENTED(66, "A direct superclass is different for the new class version, or the set of directly implemented interfaces is different and canUnrestrictedlyRedefineClasses is false."),
    DELETE_METHOD_NOT_IMPLEMENTED(67, "The new class version does not declare a method declared in the old class version and canUnrestrictedlyRedefineClasses is false."),
    UNSUPPORTED_VERSION(68, "A class file has a version number not supported by this VM."),
    NAMES_DONT_MATCH(69, "The class name defined in the new class file is different from the name in the old class object."),
    CLASS_MODIFIERS_CHANGE_NOT_IMPLEMENTED(70, "The new class version has different modifiers and and canUnrestrictedlyRedefineClasses is false."),
    METHOD_MODIFIERS_CHANGE_NOT_IMPLEMENTED(71, "A method in the new class version has different modifiers than its counterpart in the old class version and and canUnrestrictedlyRedefineClasses is false."),
    NOT_IMPLEMENTED(99, "The functionality is not implemented in this virtual machine."),
    NULL_POINTER(100, "Invalid pointer."),
    ABSENT_INFORMATION(101, "Desired information is not available."),
    INVALID_EVENT_TYPE(102, "The specified event type id is not recognized."),
    ILLEGAL_ARGUMENT(103, "Illegal argument."),
    OUT_OF_MEMORY(110, "The function needed to allocate memory and no more memory was available for allocation."),
    ACCESS_DENIED(111, "Debugging has not been enabled in this virtual machine. JVMTI cannot be used."),
    VM_DEAD(112, "The virtual machine is not running."),
    INTERNAL(113, "An unexpected internal error has occurred."),
    UNATTACHED_THREAD(115, "The thread being used to call this function is not attached to the virtual machine. Calls must be made from attached threads."),
    INVALID_TAG(500, "object type id or class tag."),
    ALREADY_INVOKING(502, "Previous invoke not complete."),
    INVALID_INDEX(503, "Index is invalid."),
    INVALID_LENGTH(504, "The length is invalid."),
    INVALID_STRING(506, "The string is invalid."),
    INVALID_CLASS_LOADER(507, "The class loader is invalid."),
    INVALID_ARRAY(508, "The array is invalid."),
    TRANSPORT_LOAD(509, "Unable to load the transport."),
    TRANSPORT_INIT(510, "Unable to initialize the transport."),
    NATIVE_METHOD(511, ""),
    INVALID_COUNT(512, "The count is invalid.");

    public final int errorCode;
    public final String errorMessage;

    ErrorCodes(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static ErrorCodes getErrorCode(int code) {
        for(ErrorCodes eCode : values()) {
            if(eCode.errorCode == code) {
                return eCode;
            }
        }
        throw new RuntimeException("Unknown Error Code: " + code);
    }
}
