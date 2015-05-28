package build.pluto.buildspoofax.util;

import java.util.ArrayList;
import java.util.Collection;

import org.metaborg.spoofax.core.transform.stratego.menu.Action;
import org.metaborg.spoofax.core.transform.stratego.menu.Menu;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;

public class MenuSerializer extends Serializer<Menu> {

	@Override
	public void write(Kryo kryo, Output output, Menu object) {
		kryo.writeObject(output, object.name());
		kryo.writeObject(output, object.actions(), new CollectionSerializer());
		kryo.writeObject(output, object.submenus(), new CollectionSerializer());
	}

	@Override
	public Menu read(Kryo kryo, Input input, Class<Menu> type) {
		String name = kryo.readObject(input, String.class);
		Collection<Action> actions = kryo.readObject(input, ArrayList.class, new CollectionSerializer());// Action.class,
																											// new
																											// ActionSerializer()));
		Collection<Menu> submenus = kryo.readObject(input, ArrayList.class, new CollectionSerializer());// Menu.class,
																										// new
																										// MenuSerializer()));
		return new Menu(name, submenus, actions);
	}

}
