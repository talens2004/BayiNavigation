package cn.msqlite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

public class SerializationUtils
{
	/**
	 * 作为工具类,将构造方法设置为Private可以防止别人New
	 */
	private SerializationUtils()
	{ }
	
	/**
	 * 序列化
	 * @param object
	 * @return
	 * @throws IOException
	 * @throws NotSerializableException
	 */
	public static byte[] serialize(Object object) throws IOException, NotSerializableException
	{
		if (object instanceof Serializable == false)
			throw new NotSerializableException();

		ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
		ObjectOutput objectOut = null;
		
		try
		{
			objectOut = new ObjectOutputStream(byteOutStream);
			objectOut.writeObject(object);
			return byteOutStream.toByteArray();
		}
		finally
		{
			byteOutStream.close();
			if (objectOut != null)
				objectOut.close();
		}		
	}
	
	/**
	 * 反序列化
	 * @param bytes
	 * @return
	 * @throws StreamCorruptedException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object deserialize(byte [] bytes) throws StreamCorruptedException, IOException, ClassNotFoundException
	{
		ByteArrayInputStream inStream = new ByteArrayInputStream(bytes);
		ObjectInput objectIn = null;
		
		try
		{
			objectIn = new ObjectInputStream(inStream);
			return objectIn.readObject();
		}
		finally 
		{
			inStream.close();
			if (objectIn != null)
				objectIn.close();
		}
	}	
}
