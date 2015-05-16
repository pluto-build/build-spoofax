package build.pluto.buildspoofax;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.spoofax.core.project.IProject;
import org.metaborg.spoofax.core.project.IProjectService;
import org.metaborg.spoofax.core.resource.ResourceService;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class SpoofaxPlutoModule extends SpoofaxModule {
	private final File baseDir;
	
	public SpoofaxPlutoModule(File baseDir) {
		super();
		this.baseDir = baseDir;
	}
	
	@Override
	protected void bindProject() {
		bindConstant().annotatedWith(Names.named("BaseDir")).to(baseDir.getAbsolutePath());
		bind(IProjectService.class).to(PlutoProjectService.class).in(Singleton.class);
	}
	
	public static class PlutoProjectService implements IProjectService {
		private static final long serialVersionUID = 3321676947613421368L;

		private final SpoofaxPlutoProject project;
		
		public @Inject PlutoProjectService(@Named("BaseDir") String baseDir) {
			try {
				this.project = new SpoofaxPlutoProject(VFS.getManager().resolveFile(baseDir));
			} catch (FileSystemException e) {
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public IProject get(final FileObject resource) {
			return project;
		}
	}
	
	public static class SpoofaxPlutoProject implements IProject, Serializable {
		private static final long serialVersionUID = 3903362242701915825L;
		private transient FileObject baseDir;

		public SpoofaxPlutoProject(FileObject baseDir) {
			this.baseDir = baseDir;
		}

		@Override
		public FileObject location() {
			return baseDir;
		}
		
		private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
			in.defaultReadObject();
			baseDir = ResourceService.readFileObject(in);
		}
		
		private void writeObject(ObjectOutputStream out) throws IOException {
			out.defaultWriteObject();
			ResourceService.writeFileObject(baseDir, out);
		}
	}
}
