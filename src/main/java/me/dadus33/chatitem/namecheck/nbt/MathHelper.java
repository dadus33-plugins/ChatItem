package me.dadus33.chatitem.namecheck.nbt;

class MathHelper
{
    static int floor_float(float value)
    {
        int i = (int)value;
        return value < (float)i ? i - 1 : i;
    }

    public static int floor_double(double value)
    {
        int i = (int)value;
        return value < (double)i ? i - 1 : i;
    }

    public static long floor_double_long(double value)
    {
        long i = (long)value;
        return value < (double)i ? i - 1L : i;
    }
}
