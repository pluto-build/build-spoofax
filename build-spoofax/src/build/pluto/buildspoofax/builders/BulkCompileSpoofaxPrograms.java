package build.pluto.buildspoofax.builders;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.BuildInputBuilder;
import org.metaborg.core.build.ConsoleBuildMessagePrinter;
import org.metaborg.core.build.IBuildOutput;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.processing.CancellationToken;
import org.metaborg.core.processing.NullProgressReporter;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.resource.ResourceChange;
import org.metaborg.core.resource.ResourceChangeKind;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.transform.CompileGoal;
import org.metaborg.spoofax.core.build.ISpoofaxBuilder;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.spoofax.core.resource.SpoofaxIgnoresSelector;
import org.metaborg.util.file.FileUtils;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

import build.pluto.builder.BuilderFactory;
import build.pluto.builder.BuilderFactoryFactory;
import build.pluto.builder.bulk.BulkBuilder;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.None;
import build.pluto.stamp.LastModifiedStamper;

import com.google.common.collect.Lists;
import com.google.inject.Injector;

public class BulkCompileSpoofaxPrograms extends BulkBuilder<BulkCompileSpoofaxPrograms.Input, None> {
    private static final ILogger logger = LoggerUtils.logger(BulkCompileSpoofaxPrograms.class);

    public static BuilderFactory<Input, BulkBuilder.BulkOutput<None>, BulkCompileSpoofaxPrograms> factory =
        BuilderFactoryFactory.of(BulkCompileSpoofaxPrograms.class, Input.class);


    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -6664040135203547324L;

        public final Collection<File> files;

        public Input(SpoofaxContext context, Collection<File> files) {
            super(context);
            this.files = files;
        }
    }


    public BulkCompileSpoofaxPrograms(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "Compiling Spoofax programs";
    }

    @Override public File persistentPath(Input input) {
        return input.context.depPath("compile.spoofax.programs.dep");
    }


    @Override protected Collection<File> requiredFiles(Input input) {
        return input.files;
    }

    @Override protected None buildBulk(Input input, Set<File> changedFiles) throws Throwable {
        for(File source : changedFiles) {
            for(File dep : input.files) {
                require(source, dep, LastModifiedStamper.instance);
            }
        }
        
        final Injector injector = input.context.injector;
        final SpoofaxProjectSettings settings = input.context.settings;
        final ISpoofaxBuilder builder = injector.getInstance(ISpoofaxBuilder.class);
        final ISourceTextService sourceTextService = injector.getInstance(ISourceTextService.class);
        final IDependencyService dependencyService = injector.getInstance(IDependencyService.class);
        final ILanguagePathService languagePathService = injector.getInstance(ILanguagePathService.class);
        final IProject project = injector.getInstance(IProjectService.class).get(settings.location());

        final Collection<ResourceChange> resourceChanges = Lists.newLinkedList();
        for(File changedFile : changedFiles) {
            final FileObject fileObject = input.context.resourceService.resolve(changedFile);
            resourceChanges.add(new ResourceChange(fileObject, ResourceChangeKind.Modify));
        }

        final BuildInputBuilder inputBuilder = new BuildInputBuilder(project);

        // @formatter:off
        final BuildInput buildInput = inputBuilder
            .withCompileDependencyLanguages(true)
            .withSourceChanges(resourceChanges)
            .withDefaultIncludePaths(true)
            .withSelector(new SpoofaxIgnoresSelector())
            .withMessagePrinter(new ConsoleBuildMessagePrinter(sourceTextService, true, true, logger))
            .withPardonedLanguageStrings(settings.pardonedLanguages())
            .addTransformGoal(new CompileGoal())
            .withThrowOnErrors(true)
            .build(dependencyService, languagePathService)
            ;
        // @formatter:on

        final IBuildOutput<IStrategoTerm, IStrategoTerm, IStrategoTerm> output =
            builder.build(buildInput, new NullProgressReporter(), new CancellationToken());

        // TODO: figure out which source files 

        return None.val;
    }
}
