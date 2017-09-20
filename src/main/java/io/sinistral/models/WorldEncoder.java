package io.sinistral.models;

public class WorldEncoder extends com.jsoniter.spi.EmptyEncoder
{
	public void encode(Object obj, com.jsoniter.output.JsonStream stream) throws java.io.IOException
	{
		if (obj == null)
		{
			stream.writeNull();
			return;
		}
		stream.writeRaw("{\"id\":", 6);
		encode_((io.sinistral.models.World) obj, stream);
		stream.writeByte(com.jsoniter.output.JsonStream.OBJECT_END);
	}

	public static void encode_(io.sinistral.models.World obj, com.jsoniter.output.JsonStream stream) throws java.io.IOException
	{
		stream.writeVal((int) obj.id);
		stream.writeRaw(",\"randomNumber\":", 16);
		stream.writeVal((int) obj.randomNumber);
	}
}
