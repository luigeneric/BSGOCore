package io.github.luigeneric.linearalgebra.utility;


import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.linearalgebra.base.Vector3;

import java.util.Arrays;

public class Matrix3x3
{
    private final float[][] interMatrix;

    public Matrix3x3(final float[][] interMatrix)
    {
        this.interMatrix = interMatrix;
    }
    public Matrix3x3()
    {
        this(new float[3][3]);
    }

    public static Matrix3x3 fromQuaternion(final Quaternion rotation)
    {
        final float x = rotation.x();
        final float y = rotation.y();
        final float z = rotation.z();
        final float w = rotation.w();

        final float xSq = x * x;
        final float ySq = y * y;
        final float zSq = z * z;

        final float m00 = 1f - 2f*ySq - 2f*zSq;
        final float m01 = 2f*x*y + 2f*w*z;
        final float m02 = 2f*x*z - 2f*w*y;

        final float m10 = 2f*x*y - 2f*w*z;
        final float m11 = 1f - 2f*xSq - 2f*zSq;
        final float m12 = 2f*y*z + 2f*w*x;

        final float m20 = 2f*x+z + 2f*w*y;
        final float m21 = 2f*y*z - 2f*w*x;
        final float m22 = 1f - 2f*xSq - 2f*ySq;

        final float[][] matrix = new float[3][3];

        matrix[0][0] = m00;
        matrix[0][1] = m01;
        matrix[0][2] = m02;

        matrix[1][0] = m10;
        matrix[1][1] = m11;
        matrix[1][2] = m12;

        matrix[2][0] = m20;
        matrix[2][1] = m21;
        matrix[2][2] = m22;

        return new Matrix3x3(matrix);
    }

    private float[][] transpose(final float[][] matrix)
    {
        float[][] result = new float[matrix[0].length][matrix.length];
        for(int i = 0; i < matrix.length; i++)
        {
            for (int j = 0; j < matrix[0].length ; j++)
            {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }
    private float[][] mult(float[][] A, float[][] B)
    {
        float[][] D = transpose(B);
        float[][] C = new float[A.length][B[0].length];
        for (int i = 0; i < A.length; i++)
        {
            for (int j = 0; j < B[0].length; j++)
            {
                C[i][j] = mult(A[i], D[j]);
            }
        }
        return C;
    }

    private float[] mult(float[][] matrix, float[] vec)
    {
        if(matrix[0].length != vec.length)
            throw new IllegalArgumentException("columns of first matrix have to match rows of vector!");

        //verpacke Vektor in Matrix
        float[][] matrixTmp = new float[vec.length][1];
        for (int i = 0; i < vec.length; i++)
        {
            matrixTmp[i][0] = vec[i];
        }

        matrixTmp = this.mult(matrix, matrixTmp);

        //entpacke Matrix in Vector
        float[] resultVector = new float[matrixTmp.length];
        for (int i = 0; i < matrixTmp.length; i++) {
            resultVector[i] = matrixTmp[i][0];
        }

        return resultVector;
    }
    private float mult(float[] vecA, float[] vecB)
    {
        float result = 0;
        for (int i = 0; i < vecA.length; i++) {
            result += vecA[i] * vecB[i];
        }
        return result;
    }

    public static Matrix3x3 transpose(final Matrix3x3 toTranspose)
    {
        final float[][] internal = toTranspose.interMatrix;
        final float[][] t = new float[3][3];
        for (int i = 0; i < internal.length; i++)
        {
            for (int j = 0; j < internal[0].length; j++)
            {
                t[j][i] = internal[i][j];
            }
        }
        return new Matrix3x3(t);
    }

    public Vector3 mult(final Vector3 v)
    {
        //performing matrix multiplication
        float[] r = this.mult(this.interMatrix, v.toArray());
        return new Vector3(r);
    }

    public float at(final int i, final int j)
    {
        return this.interMatrix[i][j];
    }

    public float getTrace()
    {
        return this.interMatrix[0][0] + this.interMatrix[1][1] + this.interMatrix[2][2];
    }

    public Matrix3x3 abs(final float EPSILON)
    {
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                this.interMatrix[i][j] = Mathf.abs(this.interMatrix[i][j]) + EPSILON;
            }
        }
        return this;
    }


    @Override
    public String toString()
    {
        return "Matrix3x3{\n\t" +
                Arrays.toString(interMatrix[0]) + "\n\t" + Arrays.toString(interMatrix[1]) + "\n\t" + Arrays.toString(interMatrix[2]) +
                '}';
    }
}
