package build.pluto.buildspoofax.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
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
		kryo.writeObject(output, lang.version());
		kryo.writeObject(output, lang.location().getName().getPath());
		kryo.writeObject(output, lang.sequenceId());
		List<ILanguageFacet> facets = new ArrayList<>();
		for (ILanguageFacet facet : lang.facets()) {
			facets.add(facet);
		}
		kryo.writeObject(output, facets.size());
		for (ILanguageFacet facet : facets) {
			// kryo.writeObject(output, facet);
		}
	}

	@Override
	public Language read(Kryo kryo, Input input, Class<Language> type) {
		String name = kryo.readObject(input, String.class);
		LanguageVersion version = kryo.readObject(input, LanguageVersion.class);
		FileObject location;
		try {
			location = VFS.getManager().resolveFile(kryo.readObject(input, String.class));
		} catch (FileSystemException e) {
			throw new RuntimeException(e);
		}
		int sequenceID = kryo.readObject(input, Integer.class);
		int numFacets = kryo.readObject(input, Integer.class);
		List<ILanguageFacet> facets = new ArrayList<>(numFacets);
		for (int i = 0; i < numFacets; i++) {
			// facets.add((ILanguageFacet) kryo.readClassAndObject(input));
		}

		Language lang = new Language(name, location, version, sequenceID);
		for (ILanguageFacet facet : facets) {
			lang.addFacet(facet);
		}

		return lang;
	}
}
