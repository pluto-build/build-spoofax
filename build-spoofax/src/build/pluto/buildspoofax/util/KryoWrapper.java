package build.pluto.buildspoofax.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.Language;
import org.metaborg.spoofax.core.language.LanguageVersion;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

public class KryoWrapper<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 125614451967087785L;

	private static ThreadLocal<Kryo> kryos = ThreadLocal.withInitial(() -> {
		Kryo kryo = new Kryo();
		kryo.register(Language.class, new LanguageSerializer());
		kryo.register(LanguageVersion.class, new LanguageVersionSerializer());
		kryo.register(FileObject.class, new JavaSerializer());
		return kryo;
	});

	private T elem;

	public KryoWrapper(T elem) {
		super();
		this.elem = elem;
	}

	public T get() {
		return elem;
	}

	private void writeObject(ObjectOutputStream stream) throws IOException {
		Kryo kryo = kryos.get();
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		Output output = new Output(bStream);
		kryo.writeClassAndObject(output, elem);
		output.close();
		byte[] kryoData = bStream.toByteArray();
		stream.writeInt(kryoData.length);
		stream.write(kryoData);
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		Kryo kryo = kryos.get();
		int numBytes = stream.readInt();
		byte[] kryoData = new byte[numBytes];
		int numRead = 0;
		while (numRead < numBytes) {
			numRead += stream.read(kryoData, numRead, numBytes - numRead);
		}
		ByteArrayInputStream bStream = new ByteArrayInputStream(kryoData);
		Input input = new Input(bStream);

		elem = (T) kryo.readClassAndObject(input);
	}

}
