package build.pluto.buildspoofax.util;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguageFacet;
import org.metaborg.spoofax.core.language.Language;
import org.metaborg.spoofax.core.language.LanguageVersion;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class LanguageSerializer extends Serializer<Language> {

	@Override
	public void write(Kryo kryo, Output output, Language lang) {
		kryo.writeObject(output, lang.name());
		kryo.writeObject(output, lang.version(), new LanguageVersionSerializer());
		kryo.writeObject(output, lang.location(), new FileObjectSerializer());
		kryo.writeObject(output, lang.sequenceId());
		kryo.writeObject(output, lang.facets(), new IterableSerializer<>());
	}

	@Override
	public Language read(Kryo kryo, Input input, Class<Language> type) {
		String name = kryo.readObject(input, String.class);
		LanguageVersion version = kryo.readObject(input, LanguageVersion.class);
		FileObject location = kryo.readObject(input, FileObject.class);
		int sequenceID = kryo.readObject(input, Integer.class);

		Iterable<ILanguageFacet> facets = kryo.readObject(input, Iterable.class, new IterableSerializer<>());
		Language lang = new Language(name, location, version, sequenceID);
		for (ILanguageFacet facet : facets) {
			lang.addFacet(facet);
		}

		return lang;
	}
}
