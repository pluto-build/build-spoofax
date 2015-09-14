package build.pluto.buildspoofax.builders;

import java.io.File;
import java.util.Collection;

import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.analysis.AnalysisResult;
import org.metaborg.util.file.FileUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

import build.pluto.builder.BuildRequest;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.dependency.FileRequirement;
import build.pluto.output.None;
import build.pluto.output.Out;
import build.pluto.output.OutputHashStamper;
import build.pluto.output.OutputStamp;
import build.pluto.output.OutputStamper;
import build.pluto.output.OutputTransient;
import build.pluto.stamp.Stamp;

import com.google.common.collect.Lists;

public class FancyTransformSpoofaxPrograms extends
    SpoofaxBuilder<FancyTransformSpoofaxPrograms.Input, Out<FancyTransformSpoofaxPrograms.Result>> {

    public static class Input extends SpoofaxInput {
        public Input(SpoofaxContext context) {
            super(context);
        }

        private static final long serialVersionUID = -3413141138547123823L;
        public final File file = null;
    }

    public static class Result {
        public final AnalysisResult<IStrategoTerm, IStrategoTerm> result = null;
    }

    @Override protected None build(final Input input) throws Throwable {
        // example on how to do
//        AnalysisResult<IStrategoTerm, IStrategoTerm> analysisResult =
//            requireBuild(new BuildRequest<>(FancyAnalyzeSpoofaxPrograms.factory, new FancyAnalyzeSpoofaxPrograms.Input(
//                context), new OutputStamper<Out<FancyAnalyzeSpoofaxPrograms.Result>>() {
//                @Override public OutputStamp<Out<FancyAnalyzeSpoofaxPrograms.Result>> stampOf(
//                    Out<FancyAnalyzeSpoofaxPrograms.Result> result) {
//                    for(AnalysisFileResult<IStrategoTerm, IStrategoTerm> fileResult : result.val().result.fileResults) {
//                        
//                        if(FileUtils.toFile(fileResult.source).equals(input.file)) {
//                            return OutputHashStamper.instance().stampOf(OutputTransient.of(result.val()));
//                        }
//                    }
//                }
//            }));



        final Collection<File> changedfiles = Lists.newLinkedList();
        if(getPreviousBuildUnit() != null) {
            for(FileRequirement freq : getPreviousBuildUnit().getRequiredFiles()) {
                final File file = freq.file;
                final Stamp oldStamp = freq.stamp;
                final Stamp newStamp = oldStamp.getStamper().stampOf(file);
                if(!oldStamp.equals(newStamp)) {
                    changedfiles.add(file);
                }
            }

            // TODO: analyze changed files
            final AnalysisResult<IStrategoTerm, IStrategoTerm> newResult = null;

            // TODO: reconstruct output, replace with new
            Result previousOutput = getPreviousBuildUnit().getBuildResult().val();

        } else
            throw new UnsupportedOperationException();

        // TODO Auto-generated method stub
        return null;
    }
}
