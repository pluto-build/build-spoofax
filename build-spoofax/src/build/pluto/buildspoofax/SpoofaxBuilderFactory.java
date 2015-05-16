package build.pluto.buildspoofax;

import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.output.Output;

@FunctionalInterface
public interface SpoofaxBuilderFactory<In extends SpoofaxInput, Out extends Output, B extends Builder<In, Out>> extends
		BuilderFactory<In, Out, B> {
}