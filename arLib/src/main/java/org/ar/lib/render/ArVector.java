
package org.ar.lib.render;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class holds information of a point in a three-dimensional coordinate
 * system. It holds the values for the x-, y- and z-axis, which can be modified
 * through several methods. (for example adding and subtracting points) The
 * distance from the origin of the coordinate system to the point represents the
 * vector. The application uses vectors to describe distances on the map.
 * 
 * @author daniele
 * 
 */
public class ArVector implements Parcelable{
	public float x;
	public float y;
	public float z;

	public ArVector() {
		this(0, 0, 0);
	}

	public ArVector(ArVector v) {
		this(v.x, v.y, v.z);
	}

	public ArVector(float v[]) {
		this(v[0], v[1], v[2]);
	}

	public ArVector(float x, float y, float z) {
		set(x, y, z);
	}

	public ArVector(Parcel in){
		readParcel(in);
	}

	public static final Parcelable.Creator<ArVector> CREATOR = new Parcelable.Creator<ArVector>() {
		public ArVector createFromParcel(Parcel in) {
			return new ArVector(in);
		}

		public ArVector[] newArray(int size) {
			return new ArVector[size];
		}
	};

	@Override
	public boolean equals(Object obj) {
		ArVector v = (ArVector) obj;
		return (v.x == x && v.y == y && v.z == z);
	}

	public boolean equals(float x, float y, float z) {
		return (this.x == x && this.y == y && this.z == z);
	}
	
	@Override
	public int hashCode() {
		Float xf = x;
		Float yf = y;
		Float zf = z;
		return xf.hashCode()+yf.hashCode()+zf.hashCode();
	}

	@Override
	public String toString() {
		return "<" + x + ", " + y + ", " + z + ">";
	}

	public void set(ArVector v) {
		set(v.x, v.y, v.z);
	}

	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
	}

	public void add(ArVector v) {
		add(v.x, v.y, v.z);
	}

	public void sub(float x, float y, float z) {
		add(-x, -y, -z);
	}

	public void sub(ArVector v) {
		add(-v.x, -v.y, -v.z);
	}

	public void mult(float s) {
		x *= s;
		y *= s;
		z *= s;
	}

	public void divide(float s) {
		x /= s;
		y /= s;
		z /= s;
	}

	public float length() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}

	public float length2D() {
		return (float) Math.sqrt(x * x + z * z);
	}

	public void norm() {
		divide(length());
	}

	public float dot(ArVector v) {
		return x * v.x + y * v.y + z * v.z;
	}

	public void cross(ArVector u, ArVector v) {
		float x = u.y * v.z - u.z * v.y;
		float y = u.z * v.x - u.x * v.z;
		float z = u.x * v.y - u.y * v.x;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void prod(Matrix m) {
		float xTemp = m.a1 * x + m.a2 * y + m.a3 * z;
		float yTemp = m.b1 * x + m.b2 * y + m.b3 * z;
		float zTemp = m.c1 * x + m.c2 * y + m.c3 * z;

		x = xTemp;
		y = yTemp;
		z = zTemp;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeFloat(x);
		dest.writeFloat(y);
		dest.writeFloat(z);
	}

	public void readParcel(Parcel in) {
		x = in.readFloat();
		y = in.readFloat();
		z = in.readFloat();
	}
}