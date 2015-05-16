package build.pluto.buildspoofax;

import build.pluto.builder.Builder;
import build.pluto.output.Output;
import build.pluto.stamp.FileHashStamper;
import build.pluto.stamp.LastModifiedStamper;
import build.pluto.stamp.Stamper;

abstract public class SpoofaxBuilder<In extends SpoofaxInput, Out extends Output> extends Builder<In, Out> {

	protected final SpoofaxContext context;
	
	public SpoofaxBuilder(In input) {
		super(input);
		this.context = input.context;
	}

	@Override
	protected Stamper defaultStamper() {
		return SpoofaxContext.BETTER_STAMPERS ? FileHashStamper.instance : LastModifiedStamper.instance;
	}
}
