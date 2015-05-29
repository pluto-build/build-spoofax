package build.pluto.buildspoofax.util;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.context.ContextIdentifier;
import org.metaborg.spoofax.core.language.ILanguage;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class ContextIdentifierSerializer extends Serializer<ContextIdentifier> {

	@Override
	public void write(Kryo kryo, Output output, ContextIdentifier object) {
		kryo.writeObject(output, object.location, kryo.getSerializer(FileObject.class));
		kryo.writeClassAndObject(output, object.language);
	}

	@Override
	public ContextIdentifier read(Kryo kryo, Input input, Class<ContextIdentifier> type) {
		FileObject file = kryo.readObject(input, FileObject.class);
		ILanguage langauge = (ILanguage) kryo.readClassAndObject(input);
		return new ContextIdentifier(file, langauge);
	}

}
