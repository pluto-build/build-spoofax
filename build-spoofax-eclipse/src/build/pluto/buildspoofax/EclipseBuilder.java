package build.pluto.buildspoofax;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import build.pluto.BuildUnit;
import build.pluto.BuildUnit.ModuleVisitor;
import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.FileRequirement;
import build.pluto.dependency.Requirement;
import build.pluto.stamp.Stamper;

/**
 * updates editors to show newly built results
 * 
 * @author Sebastian Erdweg <seba at informatik uni-marburg de>
 */
public class EclipseBuilder extends IncrementalProjectBuilder {

	
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) {
		File projectPath = getProject().getProjectRelativePath().toFile();
		// TODO get guice injector and project settings
		Main.Input input = new Main.Input(null, null);

		try {
			BuildManagers.build(new BuildRequest<>(Main.factory, input)));
//			logFileStatistics(new BuildRequest<>(All.factory, input));
		} finally {
			monitor.done();
			try {
				getProject().refreshLocal(IProject.DEPTH_INFINITE, monitor);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private <K> void mapInc(Map<K, Integer> map, K key) {
		Integer i = map.get(key);
		if (i == null)
			map.put(key, 1);
		else
			map.put(key, i+1);
	}

	private void logFileStatistics(BuildRequest<?,?,?,?> req) {
		try {
			BuildUnit<?> result = BuildManagers.readResult(req);
			
			final List<File> requiredFiles = new ArrayList<>();
			final List<File> providedFiles = new ArrayList<>();
			final Map<Class<? extends Stamper>, Integer> requireStampers = new HashMap<>();
			final Map<Class<? extends Stamper>, Integer> providerStampers = new HashMap<>();

			result.visit(new ModuleVisitor<Void>() {
				@Override
				public Void visit(BuildUnit<?> mod) {
					providedFiles.addAll(mod.getGeneratedFiles());
					
					for (FileRequirement freq : mod.getGeneratedFileRequirements())
						mapInc(providerStampers, freq.stamp.getStamper().getClass());
					
					for (Requirement req : mod.getRequirements())
						if (req instanceof FileRequirement) {
							mapInc(requireStampers, ((FileRequirement) req).stamp.getStamper().getClass());
							requiredFiles.add(((FileRequirement) req).file);
						}
					
					return null;
				}

				@Override
				public Void combine(Void t1, Void t2) {
					return null;
				}

				@Override
				public Void init() {
					return null;
				}

				@Override
				public boolean cancel(Void t) {
					return false;
				}
			});
			
			int nongeneratedRequired = 0;
			int generatedRequired = 0;
			for (File p : requiredFiles)
				if (providedFiles.contains(p))
					generatedRequired++;
				else
					nongeneratedRequired++;
			
			System.out.println("Generated " + providedFiles.size() + " files.");
			System.out.println("Required " + generatedRequired + " generated files.");
			System.out.println("Required " + nongeneratedRequired + " nongenerated files.");
			System.out.println("Requiremenet stampers: " + requireStampers);
			System.out.println("Provision stampers: " + providerStampers);
		} catch (IOException e) {
		}		
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		SpoofaxContext context = SpoofaxContext.makeContext(getProject().getProjectRelativePath().toFile());
		SpoofaxInput input = new SpoofaxInput(context);
		try {
			BuildManagers.build(new BuildRequest<>(Clean.factory, input));
		} finally {
			monitor.done();
			try {
				getProject().refreshLocal(IProject.DEPTH_INFINITE, monitor);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
}
