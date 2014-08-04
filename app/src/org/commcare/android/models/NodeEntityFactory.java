/**
 * 
 */
package org.commcare.android.models;

import java.util.Enumeration;
import java.util.Hashtable;

import org.commcare.android.database.user.models.User;
import org.commcare.android.util.SessionUnavailableException;
import org.commcare.suite.model.Detail;
import org.commcare.suite.model.DetailField;
import org.commcare.suite.model.Text;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.XPathException;
import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;

/**
 * @author ctsims
 *
 */
public class NodeEntityFactory {

	private EvaluationContext ec;
	
	Detail detail;
	FormInstance instance;
	User current; 
	
	public Detail getDetail() {
		return detail;
	}

	
	public NodeEntityFactory(Detail d, EvaluationContext ec) {
		this.detail = d;
		this.ec = ec;
	}

	public Entity<TreeReference> getEntity(TreeReference data) throws SessionUnavailableException {
		EvaluationContext nodeContext = new EvaluationContext(ec, data);
		Hashtable<String, XPathExpression> variables = getDetail().getVariableDeclarations();
		//These are actually in an ordered hashtable, so we can't just get the keyset, since it's
		//in a 1.3 hashtable equivilant
		for(Enumeration<String> en = variables.keys(); en.hasMoreElements();) {
			String key = en.nextElement();
			nodeContext.setVariable(key, XPathFuncExpr.unpack(variables.get(key).eval(nodeContext)));
		}
		
		//return new AsyncEntity<TreeReference>(detail.getFields(), nodeContext, data);
		
		int length = detail.getHeaderForms().length;
		String[] details = new String[length];
		String[] sortDetails = new String[length];
		boolean[] relevancyDetails = new boolean[length];
		int count = 0;
		for(DetailField f : this.getDetail().getFields()) {
			try {
				details[count] = f.getTemplate().evaluate(nodeContext);
				Text sortText = f.getSort();
				if(sortText == null) {
					sortDetails[count] = details[count];
				} else {
					sortDetails[count] = sortText.evaluate(nodeContext);
				}
				String relevancy = f.getRelevancy();
				System.out.println("[jls] header => " + f.getHeader().evaluate(nodeContext) + ", relevancy => " + f.getRelevancy());
				boolean isRelevant = true;
				if (relevancy != null) {
					try {
						XPathExpression parsed = XPathParseTool.parseXPath(relevancy);
						isRelevant = XPathFuncExpr.toBoolean(parsed.eval(nodeContext)).booleanValue();
					} catch (XPathSyntaxException e) {
						e.printStackTrace();
					}
				}
				relevancyDetails[count] = isRelevant;
			} catch(XPathException xpe) {
				xpe.printStackTrace();
				details[count] = "<invalid xpath: " + xpe.getMessage() + ">";
			}
			count++;
		}
		
		return new Entity<TreeReference>(details, sortDetails, relevancyDetails, data);
	}
}
