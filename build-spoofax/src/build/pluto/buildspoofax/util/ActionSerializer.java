package build.pluto.buildspoofax.util;

import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.transform.stratego.menu.Action;
import org.metaborg.spoofax.core.transform.stratego.menu.ActionFlags;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class ActionSerializer extends Serializer<Action> {

	@Override
	public void write(Kryo kryo, Output output, Action action) {
		kryo.writeObject(output, action.name);
		kryo.writeObjectOrNull(output, action.inputLangauge, ILanguage.class);
		kryo.writeObjectOrNull(output, action.outputLanguage, ILanguage.class);
		kryo.writeObject(output, action.strategy);
		kryo.writeObject(output, action.flags);
	}

	@Override
	public Action read(Kryo kryo, Input input, Class<Action> type) {
		String name = kryo.readObject(input, String.class);
		ILanguage inputLanguage = kryo.readObjectOrNull(input, ILanguage.class);
		ILanguage outputLanguage = kryo.readObjectOrNull(input, ILanguage.class);
		String strategy = kryo.readObject(input, String.class);
		ActionFlags flags = kryo.readObject(input, ActionFlags.class);
		return new Action(name, inputLanguage, outputLanguage, strategy, flags);
	}

}
