package eu.siacs.p2;

import eu.siacs.p2.pojo.Target;

public interface PushService {

    boolean push(Target target, boolean highPriority) throws TargetDeviceNotFoundException;
}
