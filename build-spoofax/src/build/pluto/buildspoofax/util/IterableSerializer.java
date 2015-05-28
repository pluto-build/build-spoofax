package build.pluto.buildspoofax.util;

import java.util.ArrayList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;

public class IterableSerializer<T> extends Serializer<Iterable<T>> {

	private Class<T> clazz;
	private Serializer<T> serializer;

	public IterableSerializer() {
		this(null, null);
	}

	public IterableSerializer(Class<T> clazz, Serializer<T> serializer) {
		super();
		this.clazz = clazz;
		this.serializer = serializer;
	}

	private static <T> ArrayList<T> iterableToList(Iterable<T> iter) {
		ArrayList<T> list = new ArrayList<>();
		for (T elem : iter) {
			list.add(elem);
		}
		return list;
	}

	@Override
	public void write(Kryo kryo, Output output, Iterable<T> object) {
		kryo.writeObject(output, iterableToList(object), new CollectionSerializer(clazz, serializer));
	}

	@Override
	public Iterable<T> read(Kryo kryo, Input input, Class<Iterable<T>> type) {
		return kryo.readObject(input, ArrayList.class, new CollectionSerializer(clazz, serializer));
	}

}
