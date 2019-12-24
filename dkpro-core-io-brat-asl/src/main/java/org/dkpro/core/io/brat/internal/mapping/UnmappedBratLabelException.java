package org.dkpro.core.io.brat.internal.mapping;

public class UnmappedBratLabelException extends Exception {
    public UnmappedBratLabelException(String mess, Exception e) { super(mess, e); }

    public UnmappedBratLabelException(String mess) { super(mess); }

    public UnmappedBratLabelException(Exception e) { super(e); }
}
