package build.pluto.buildspoofax.util;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.syntax.FenceCharacters;
import org.metaborg.spoofax.core.syntax.MultiLineCommentCharacters;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SyntaxFacetSerializer extends Serializer<SyntaxFacet> {


	@Override
	public void write(Kryo kryo, Output output, SyntaxFacet object) {
		kryo.writeObject(output, object.parseTable, new FileObjectSerializer());
		kryo.writeObject(output, object.fenceCharacters, new IterableSerializer<>());
		kryo.writeObject(output, object.multiLineCommentCharacters, new IterableSerializer<>());
		kryo.writeObject(output, object.singleLineCommentPrefixes, new IterableSerializer<>());
		kryo.writeObject(output, object.startSymbols, new IterableSerializer<>());
	}

	@Override
	public SyntaxFacet read(Kryo kryo, Input input, Class<SyntaxFacet> type) {
		FileObject parseTable = kryo.readObject(input, FileObject.class);
		Iterable<FenceCharacters> fenceChars = kryo.readObject(input, Iterable.class, new IterableSerializer<>(FenceCharacters.class, null));
		Iterable<MultiLineCommentCharacters> multiLineChars = kryo.readObject(input, Iterable.class, new IterableSerializer<>());
		Iterable<String> sigleLinePrefixes = kryo.readObject(input, Iterable.class, new IterableSerializer<>());
		Iterable<String> startSymbols = kryo.readObject(input, Iterable.class, new IterableSerializer<>());

		return new SyntaxFacet(parseTable, startSymbols, sigleLinePrefixes, multiLineChars, fenceChars);
	}

}
