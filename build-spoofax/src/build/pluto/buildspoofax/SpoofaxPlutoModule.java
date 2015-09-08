package build.pluto.buildspoofax;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.spoofax.core.SpoofaxModule;

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
	
	public static class SpoofaxPlutoProject implements IProject {
		private transient FileObject baseDir;

		public SpoofaxPlutoProject(FileObject baseDir) {
			this.baseDir = baseDir;
		}

		@Override
		public FileObject location() {
			return baseDir;
		}
//		
//		private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
//			in.defaultReadObject();
//			baseDir = ResourceService.readFileObject(in);
//		}
//		
//		private void writeObject(ObjectOutputStream out) throws IOException {
//			out.defaultWriteObject();
//			ResourceService.writeFileObject(baseDir, out);
//		}
	}
}
