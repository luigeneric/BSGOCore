package io.github.luigeneric.templates.utils;


import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.linearalgebra.base.Vector3;

public abstract class Desc implements IProtocolWrite
{
    private final String name;
    private final String title;
    protected Vector3 position;
    protected Quaternion rotation;


    protected Desc(String name)
    {
        this.name = name;
        this.title = name;
        this.position = Vector3.zero();
        this.rotation = Quaternion.identity();
    }
}
