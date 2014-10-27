package ch.ethz.globis.distindex.operation;

public class OpCode {

    public static final byte PUT = 1;
    public static final byte GET = 2;
    public static final byte GET_RANGE = 3;
    public static final byte GET_KNN = 4;
    public static final byte GET_BATCH = 5;

    public static final byte CREATE_INDEX = 6;
}