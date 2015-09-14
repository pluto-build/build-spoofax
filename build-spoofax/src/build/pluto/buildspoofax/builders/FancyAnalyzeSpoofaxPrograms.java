package build.pluto.buildspoofax.builders;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.metaborg.core.analysis.AnalysisResult;
import org.metaborg.core.syntax.ParseResult;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Lists;

import build.pluto.builder.BuildRequest;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.dependency.FileRequirement;
import build.pluto.output.IgnoreOutputStamper;
import build.pluto.output.None;
import build.pluto.output.Out;
import build.pluto.output.OutputTransient;
import build.pluto.stamp.Stamp;

public class FancyAnalyzeSpoofaxPrograms extends SpoofaxBuilder<FancyAnalyzeSpoofaxPrograms.Input, Out<FancyAnalyzeSpoofaxPrograms.Result>> {

    public static class Input extends SpoofaxInput {
        public Input(SpoofaxContext context) {
            super(context);
        }
        private static final long serialVersionUID = -3413141138547123823L;
        public final List<File> files = null;
    }
    
    public static class Result {
        public final AnalysisResult<IStrategoTerm, IStrategoTerm> result = null;
    }
    
    @Override protected None build(Input input) throws Throwable {
        
        Map<File, ParseResult<IStrategoTerm>> parseResults = new HashMap<>();
        for (File f : input.files) {
            require(f);
            Out<ParseResult<IStrategoTerm>> out = requireBuild(
                new BuildRequest<>(
                    FancyParseSpoofaxPrograms.factory, 
                    new FancyParseSpoofaxPrograms.Input(context, f), null),
                    IgnoreOutputStamper.instance
                );
            
            parseResults.put(f, out.val());
        }
        
        final Collection<File> changedfiles = Lists.newLinkedList();
        if (getPreviousBuildUnit() != null) {            
            for (FileRequirement freq : getPreviousBuildUnit().getRequiredFiles()) {
                final File file = freq.file;
                final Stamp oldStamp = freq.stamp;
                final Stamp newStamp = oldStamp.getStamper().stampOf(file);
                if(!oldStamp.equals(newStamp)) {
                    changedfiles.add(file);
                }
            }
            
            // TODO: analyze changed files
            final AnalysisResult<IStrategoTerm, IStrategoTerm> newResult = null;
            
            return OutputTransient.of(new Result(newResult));
        }
        else
            throw new UnsupportedOperationException();
        
        // TODO Auto-generated method stub
        return null;
    }

}
