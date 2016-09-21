package de.uni_hildesheim.sse.smell;

public class NullProgressPrinter implements IProgressPrinter {

    @Override
    public void start(String filtername, int estimatedCount) {
    }

    @Override
    public void start(Object filter, int estimatedCount) {
    }

    @Override
    public void finishedOne() {
    }

}
