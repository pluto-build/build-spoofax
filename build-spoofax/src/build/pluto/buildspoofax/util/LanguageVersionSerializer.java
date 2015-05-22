package build.pluto.buildspoofax.util;

import org.metaborg.spoofax.core.language.LanguageVersion;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class LanguageVersionSerializer extends Serializer<LanguageVersion> {

	@Override
	public void write(Kryo kryo, Output output, LanguageVersion object) {
		kryo.writeObject(output, object.major);
		kryo.writeObject(output, object.minor);
		kryo.writeObject(output, object.patch);
		kryo.writeObject(output, object.qualifier);
	}

	@Override
	public LanguageVersion read(Kryo kryo, Input input, Class<LanguageVersion> type) {
		int major = kryo.readObject(input, Integer.class);
		int minor = kryo.readObject(input, Integer.class);
		int patch = kryo.readObject(input, Integer.class);
		int qualifier = kryo.readObject(input, Integer.class);
		return new LanguageVersion(major, minor, patch, qualifier);
	}

}
