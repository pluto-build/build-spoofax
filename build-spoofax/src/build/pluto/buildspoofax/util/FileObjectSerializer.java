package build.pluto.buildspoofax.util;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class FileObjectSerializer extends Serializer<FileObject> {

	@Override
	public void write(Kryo kryo, Output output, FileObject object) {
		String uri = object.getName().getURI();
		kryo.writeObject(output, new String(uri));
	}

	@Override
	public FileObject read(Kryo kryo, Input input, Class<FileObject> type) {
		try {
			return VFS.getManager().resolveFile(kryo.readObject(input, String.class));
		} catch (FileSystemException e) {
			throw new KryoException(e);
		}
	}

}
