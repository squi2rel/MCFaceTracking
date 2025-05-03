package com.github.squi2rel.mcft;

import com.github.squi2rel.mcft.tracking.EyeTrackingRect;
import com.github.squi2rel.mcft.tracking.MouthTrackingRect;

public class FTModel {
    public static EyeTrackingRect eyeR = new EyeTrackingRect(-3 + 4, -1 + 8, 2, 1);
    public static EyeTrackingRect eyeL = new EyeTrackingRect(1 + 4, -1 + 8, 2, 1);
    public static MouthTrackingRect mouth = new MouthTrackingRect(-0.75f + 4, -0.25f + 8, 1.5f, 0.35f);

    public static void update() {
        eyeR.update();
        eyeL.update();
        mouth.update();
    }
}
