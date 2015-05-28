package build.pluto.buildspoofax.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.analysis.stratego.StrategoFacet;
import org.metaborg.spoofax.core.context.ContextFacet;
import org.metaborg.spoofax.core.language.IdentificationFacet;
import org.metaborg.spoofax.core.language.Language;
import org.metaborg.spoofax.core.language.LanguageVersion;
import org.metaborg.spoofax.core.language.ResourceExtensionFacet;
import org.metaborg.spoofax.core.style.Style;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.spoofax.core.transform.stratego.menu.Action;
import org.metaborg.spoofax.core.transform.stratego.menu.Menu;
import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Kryo.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.Registration;
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
		kryo.register(FileObject.class, new FileObjectSerializer());
		kryo.register(Language.class, new LanguageSerializer());
		kryo.register(LanguageVersion.class, new LanguageVersionSerializer());
		kryo.register(Style.class, new JavaSerializer());
		kryo.register(StrategoFacet.class, new JavaSerializer());
		kryo.register(IdentificationFacet.class, new JavaSerializer());
		kryo.register(ContextFacet.class, new JavaSerializer());
		kryo.register(Action.class, new ActionSerializer());
		kryo.register(ResourceExtensionFacet.class, new JavaSerializer());
		kryo.register(SyntaxFacet.class, new SyntaxFacetSerializer());
		kryo.register(Menu.class, new MenuSerializer());
		kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
		return kryo;
	});

	private T elem;
	private Class<T> clazz;

	public KryoWrapper(T elem, Class<T> clazz) {
		super();
		this.elem = elem;
		this.clazz = clazz;
	}

	public KryoWrapper(T elem) {
		super();
		this.elem = elem;
		this.clazz = null;
	}

	public T get() {
		return elem;
	}

	private void writeObject(ObjectOutputStream stream) throws IOException {
		Kryo kryo = kryos.get();
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		Output output = new Output(bStream);
		kryo.writeClass(output, clazz);
		if (clazz != null) {
			kryo.writeObject(output, elem, kryo.getSerializer(clazz));
		} else {
			kryo.writeClassAndObject(output, elem);
		}
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

		Registration reg = kryo.readClass(input);
		if (reg == null) {
			elem = (T) kryo.readClassAndObject(input);
		} else {
			clazz = (Class<T>) reg.getType();
			elem = (T) kryo.readObject(input, clazz);
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((elem == null) ? 0 : elem.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KryoWrapper other = (KryoWrapper) obj;
		if (elem == null) {
			if (other.elem != null) {
				return false;
			}
		} else {
			if (!elem.equals(other.elem)) {
				return false;
			}
		}
		return true;
	}

}
