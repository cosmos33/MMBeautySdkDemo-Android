package im.zego.customrender.orientation;

public class BeautySdkOrientationSwitchListener extends OrientationSwitchListener {
    private float lastAngle = 0;
    private float currentAngle;


    @Override
    protected void toLeft() {
        lastAngle = 90;
        sync();
    }

    @Override
    protected void toRight() {
        lastAngle = -90;
        sync();
    }

    @Override
    protected void toNormal() {
        lastAngle = 0;
        sync();
    }

    @Override
    protected void fromLeftToRight() {
        lastAngle = -90;
        sync();
    }

    @Override
    protected void fromRightToLeft() {
        lastAngle = 90;
        sync();
    }

    protected void sync() {
        currentAngle = lastAngle == 0 ? 0 : lastAngle + 180;
    }

    @Override
    protected long getDelayTime() {
        return 1000;
    }

    public float getCurrentAngle() {
        return currentAngle;
    }
}
