package com.redhat.pantheon.upgrade;

import java.io.IOException;

abstract class AppendableDecorator implements Appendable {

    private final Appendable delegate;

    public AppendableDecorator(Appendable delegate) {
        this.delegate = delegate;
    }

    public abstract CharSequence decorate(CharSequence csq);

    @Override
    public Appendable append(CharSequence csq) throws IOException {
        return delegate.append(decorate(csq));
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        return delegate.append(decorate(csq), start, end);
    }

    @Override
    public Appendable append(char c) throws IOException {
        return append(c);
    }
}
