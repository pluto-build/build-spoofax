package build.pluto.buildspoofax.stampers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermVisitor;
import org.sugarj.common.FileCommands;
import org.sugarj.common.util.Pair;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;
import build.pluto.buildspoofax.StrategoExecutor;
import build.pluto.output.OutputPersisted;
import build.pluto.stamp.LastModifiedStamper;
import build.pluto.stamp.Stamp;
import build.pluto.stamp.Stamper;
import build.pluto.stamp.ValueStamp;

public class Sdf2ParenthesizeStamper implements Stamper {
	private static final long serialVersionUID = 3294157251470549994L;
	
	private final BuildRequest<?, OutputPersisted<IStrategoTerm>, ?, ?> parseSdfDefinition;
	
	public Sdf2ParenthesizeStamper(BuildRequest<?, OutputPersisted<IStrategoTerm>, ?, ?> parseSdfDefinition) {
		this.parseSdfDefinition = parseSdfDefinition;
	}

	@Override
	public Stamp stampOf(File p) {
		if (!FileCommands.exists(p))
			return new ValueStamp<>(this, null);

		OutputPersisted<IStrategoTerm> term = BuildManagers.build(parseSdfDefinition);
		
		if (term == null || term.val == null)
			return LastModifiedStamper.instance.stampOf(p);
		
		ITermFactory factory = StrategoExecutor.strategoSdfcontext().getFactory();
		ParenExtractor parenExtractor = new ParenExtractor(factory);
		parenExtractor.visit(term.val);
		return new ValueStamp<>(this, Pair.create(parenExtractor.getRelevantProds(), parenExtractor.getPriorities()));
	}

	private static class ParenExtractor extends TermVisitor {
		private final Set<IStrategoTerm> relevantProds;
		private final Set<IStrategoTerm> priorities;

		private final ITermFactory factory;
		private final IStrategoTerm noAttrs;
		private List<IStrategoTerm> prods;

		private boolean inPriorities = false;
		
		public ParenExtractor(ITermFactory factory) {
			this.factory = factory;
			this.relevantProds = new HashSet<>();
			this.priorities = new HashSet<>();
			this.noAttrs = factory.makeAppl(factory.makeConstructor("attrs", 1), factory.makeList());
			this.prods = new ArrayList<>();
		}

		@Override
		public void preVisit(IStrategoTerm term) {
			if (term instanceof IStrategoAppl)
				switch (((IStrategoAppl) term).getConstructor().getName()) {
				case "context-free-priorities":
				case "priorities":
					priorities.add(term);
					inPriorities = true;
					break;
				case "prod":
					if (inPriorities) {
						relevantProds.add(term);
						relevantProds.add(noProdAttrs(term));
					}
					else {
						prods.add(term);
					}
				default:
					break;
				}
		}
		
		private IStrategoTerm noProdAttrs(IStrategoTerm term) {
			return factory.makeAppl(factory.makeConstructor("prod", 3), 
					term.getSubterm(0),
					term.getSubterm(1),
					noAttrs);
		}

		@Override
		public void postVisit(IStrategoTerm term) {
			if (term instanceof IStrategoAppl)
				switch (((IStrategoAppl) term).getConstructor().getName()) {
				case "context-free-priorities":
				case "priorities":
					inPriorities = false;
					break;
				default:
					break;
				}
		}
		
		public Set<IStrategoTerm> getPriorities() {
			return priorities;
		}

		public Set<IStrategoTerm> getRelevantProds() {
			if (prods != null) 
				for (IStrategoTerm prod : prods)
					if (relevantProds.contains(noProdAttrs(prod)))
						relevantProds.add(prod);
			prods = null;
			return relevantProds;
		}

	}
}
