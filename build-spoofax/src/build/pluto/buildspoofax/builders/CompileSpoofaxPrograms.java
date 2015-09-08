package build.pluto.buildspoofax.builders;

import java.io.File;

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
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.transform.CompileGoal;
import org.metaborg.spoofax.core.build.ISpoofaxBuilder;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.spoofax.core.resource.SpoofaxIgnoresSelector;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.None;

import com.google.inject.Injector;

public class CompileSpoofaxPrograms extends SpoofaxBuilder<SpoofaxInput, None> {
    private static final ILogger logger = LoggerUtils.logger(CompileSpoofaxPrograms.class);

    public static SpoofaxBuilderFactory<SpoofaxInput, None, CompileSpoofaxPrograms> factory = SpoofaxBuilderFactory.of(
        CompileSpoofaxPrograms.class, SpoofaxInput.class);

    public CompileSpoofaxPrograms(SpoofaxInput input) {
        super(input);
    }


    @Override protected String description(SpoofaxInput input) {
        return "Compiling Spoofax programs";
    }

    @Override protected File persistentPath(SpoofaxInput input) {
        return context.depPath("compile.spoofax.programs.dep");
    }

    @Override protected None build(SpoofaxInput input) throws Throwable {
        final Injector injector = context.injector();
        final SpoofaxProjectSettings settings = context.settings;
        final ISpoofaxBuilder builder = injector.getInstance(ISpoofaxBuilder.class);
        final ISourceTextService sourceTextService = injector.getInstance(ISourceTextService.class);
        final IDependencyService dependencyService = injector.getInstance(IDependencyService.class);
        final ILanguagePathService languagePathService = injector.getInstance(ILanguagePathService.class);
        final IProject project = injector.getInstance(IProjectService.class).get(settings.location());

        final BuildInputBuilder inputBuilder = new BuildInputBuilder(project);

        // @formatter:off
        final BuildInput buildInput = inputBuilder
            .withCompileDependencyLanguages(true)
            .withSourcesFromDefaultSourceLocations(true)
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

        return None.val;
    }
}
