package org.usfirst.frc948.smartdashboard.extensions;

public class ColorRange {
    
    public final int R_min;
    public final int R_max;
    public final int G_min;
    public final int G_max;
    public final int B_min;
    public final int B_max;
    
    public ColorRange(int R_min, int G_min, int B_min, int R_max, int G_max, int B_max) {
        this.R_min = R_min;
        this.G_min = G_min;
        this.B_min = B_min;
        this.R_max = R_max;
        this.G_max = G_max;
        this.B_max = B_max;
    }
    
}
