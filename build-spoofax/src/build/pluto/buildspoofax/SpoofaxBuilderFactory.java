package build.pluto.buildspoofax;

import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.builder.BuilderFactoryFactory;
import build.pluto.output.Output;

public interface SpoofaxBuilderFactory<In extends SpoofaxInput, Out extends Output, B extends Builder<In, Out>> extends
		BuilderFactory<In, Out, B> {

	public static <In extends SpoofaxInput, Out extends Output, B extends Builder<In, Out>> SpoofaxBuilderFactory<In, Out, B> of(Class<B> builderClass,
			Class<In> inputClass) {
		class GeneratedSpoofaxBuilderFactory implements SpoofaxBuilderFactory<In, Out, B> {
			private static final long serialVersionUID = -8054132810629331468L;

			private BuilderFactory<In, Out, B> factory = BuilderFactoryFactory.of(builderClass, inputClass);

			@Override
			public B makeBuilder(In input) {
				return factory.makeBuilder(input);
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((factory == null) ? 0 : factory.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				GeneratedSpoofaxBuilderFactory other = (GeneratedSpoofaxBuilderFactory) obj;
				if (factory == null) {
					if (other.factory != null)
						return false;
				} else if (!factory.equals(other.factory))
					return false;
				return true;
			}
			
			@Override
			public String toString() {
				return factory.toString();
			}

		};
		return new GeneratedSpoofaxBuilderFactory();
	}

}