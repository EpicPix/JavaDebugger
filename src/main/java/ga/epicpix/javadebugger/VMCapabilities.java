package ga.epicpix.javadebugger;

// https://docs.oracle.com/javase/8/docs/platform/jpda/jdwp/jdwp-protocol.html#JDWP_VirtualMachine_CapabilitiesNew
public class VMCapabilities {

    public final boolean canWatchFieldModification;        // Can the VM watch field modification, and therefore can it send the Modification Watchpoint Event?
    public final boolean canWatchFieldAccess;              // Can the VM watch field access, and therefore can it send the Access Watchpoint Event?
    public final boolean canGetBytecodes;                  // Can the VM get the bytecodes of a given method?
    public final boolean canGetSyntheticAttribute;         // Can the VM determine whether a field or method is synthetic? (that is, can the VM determine if the method or the field was invented by the compiler?)
    public final boolean canGetOwnedMonitorInfo;           // Can the VM get the owned monitors infornation for a thread?
    public final boolean canGetCurrentContendedMonitor;    // Can the VM get the current contended monitor of a thread?
    public final boolean canGetMonitorInfo;                // Can the VM get the monitor information for a given object?
    public final boolean canRedefineClasses;               // Can the VM redefine classes?
    public final boolean canAddMethod;                     // Can the VM add methods when redefining classes?
    public final boolean canUnrestrictedlyRedefineClasses; // Can the VM redefine classesin arbitrary ways?
    public final boolean canPopFrames;                     // Can the VM pop stack frames?
    public final boolean canUseInstanceFilters;            // Can the VM filter events by specific object?
    public final boolean canGetSourceDebugExtension;       // Can the VM get the source debug extension?
    public final boolean canRequestVMDeathEvent;           // Can the VM request VM death events?
    public final boolean canSetDefaultStratum;             // Can the VM set a default stratum?
    public final boolean canGetInstanceInfo;               // Can the VM return instances, counts of instances of classes and referring objects?
    public final boolean canRequestMonitorEvents;          // Can the VM request monitor events?
    public final boolean canGetMonitorFrameInfo;           // Can the VM get monitors with frame depth info?
    public final boolean canUseSourceNameFilters;          // Can the VM filter class prepare events by source name?
    public final boolean canGetConstantPool;               // Can the VM return the constant pool information?
    public final boolean canForceEarlyReturn;              // Can the VM force early return from a method?
    public final boolean reserved22;                       // Reserved for future capability
    public final boolean reserved23;                       // Reserved for future capability
    public final boolean reserved24;                       // Reserved for future capability
    public final boolean reserved25;                       // Reserved for future capability
    public final boolean reserved26;                       // Reserved for future capability
    public final boolean reserved27;                       // Reserved for future capability
    public final boolean reserved28;                       // Reserved for future capability
    public final boolean reserved29;                       // Reserved for future capability
    public final boolean reserved30;                       // Reserved for future capability
    public final boolean reserved31;                       // Reserved for future capability
    public final boolean reserved32;                       // Reserved for future capability

    public VMCapabilities(int caps) {
        canWatchFieldModification        = ((caps >> 0) & 1) == 1;
        canWatchFieldAccess              = ((caps >> 1) & 1) == 1;
        canGetBytecodes                  = ((caps >> 2) & 1) == 1;
        canGetSyntheticAttribute         = ((caps >> 3) & 1) == 1;
        canGetOwnedMonitorInfo           = ((caps >> 4) & 1) == 1;
        canGetCurrentContendedMonitor    = ((caps >> 5) & 1) == 1;
        canGetMonitorInfo                = ((caps >> 6) & 1) == 1;
        canRedefineClasses               = ((caps >> 7) & 1) == 1;
        canAddMethod                     = ((caps >> 8) & 1) == 1;
        canUnrestrictedlyRedefineClasses = ((caps >> 9) & 1) == 1;
        canPopFrames                     = ((caps >> 10) & 1) == 1;
        canUseInstanceFilters            = ((caps >> 11) & 1) == 1;
        canGetSourceDebugExtension       = ((caps >> 12) & 1) == 1;
        canRequestVMDeathEvent           = ((caps >> 13) & 1) == 1;
        canSetDefaultStratum             = ((caps >> 14) & 1) == 1;
        canGetInstanceInfo               = ((caps >> 15) & 1) == 1;
        canRequestMonitorEvents          = ((caps >> 16) & 1) == 1;
        canGetMonitorFrameInfo           = ((caps >> 17) & 1) == 1;
        canUseSourceNameFilters          = ((caps >> 18) & 1) == 1;
        canGetConstantPool               = ((caps >> 19) & 1) == 1;
        canForceEarlyReturn              = ((caps >> 20) & 1) == 1;
        reserved22                       = ((caps >> 21) & 1) == 1;
        reserved23                       = ((caps >> 22) & 1) == 1;
        reserved24                       = ((caps >> 23) & 1) == 1;
        reserved25                       = ((caps >> 24) & 1) == 1;
        reserved26                       = ((caps >> 25) & 1) == 1;
        reserved27                       = ((caps >> 26) & 1) == 1;
        reserved28                       = ((caps >> 27) & 1) == 1;
        reserved29                       = ((caps >> 28) & 1) == 1;
        reserved30                       = ((caps >> 29) & 1) == 1;
        reserved31                       = ((caps >> 30) & 1) == 1;
        reserved32                       = ((caps >> 31) & 1) == 1;
    }

    public void Print() {
        System.out.println("canWatchFieldModification: " + canWatchFieldModification);
        System.out.println("canWatchFieldAccess: " + canWatchFieldAccess);
        System.out.println("canGetBytecodes: " + canGetBytecodes);
        System.out.println("canGetSyntheticAttribute: " + canGetSyntheticAttribute);
        System.out.println("canGetOwnedMonitorInfo: " + canGetOwnedMonitorInfo);
        System.out.println("canGetCurrentContendedMonitor: " + canGetCurrentContendedMonitor);
        System.out.println("canGetMonitorInfo: " + canGetMonitorInfo);
        System.out.println("canRedefineClasses: " + canRedefineClasses);
        System.out.println("canAddMethod: " + canAddMethod);
        System.out.println("canUnrestrictedlyRedefineClasses: " + canUnrestrictedlyRedefineClasses);
        System.out.println("canPopFrames: " + canPopFrames);
        System.out.println("canUseInstanceFilters: " + canUseInstanceFilters);
        System.out.println("canGetSourceDebugExtension: " + canGetSourceDebugExtension);
        System.out.println("canRequestVMDeathEvent: " + canRequestVMDeathEvent);
        System.out.println("canSetDefaultStratum: " + canSetDefaultStratum);
        System.out.println("canGetInstanceInfo: " + canGetInstanceInfo);
        System.out.println("canRequestMonitorEvents: " + canRequestMonitorEvents);
        System.out.println("canGetMonitorFrameInfo: " + canGetMonitorFrameInfo);
        System.out.println("canUseSourceNameFilters: " + canUseSourceNameFilters);
        System.out.println("canGetConstantPool: " + canGetConstantPool);
        System.out.println("canForceEarlyReturn: " + canForceEarlyReturn);
        System.out.println("reserved22: " + reserved22);
        System.out.println("reserved23: " + reserved23);
        System.out.println("reserved24: " + reserved24);
        System.out.println("reserved25: " + reserved25);
        System.out.println("reserved26: " + reserved26);
        System.out.println("reserved27: " + reserved27);
        System.out.println("reserved28: " + reserved28);
        System.out.println("reserved29: " + reserved29);
        System.out.println("reserved30: " + reserved30);
        System.out.println("reserved31: " + reserved31);
        System.out.println("reserved32: " + reserved32);
    }

}
