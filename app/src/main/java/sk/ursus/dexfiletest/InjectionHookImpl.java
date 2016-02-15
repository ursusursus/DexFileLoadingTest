package sk.ursus.dexfiletest;

/**
 * Created by ursusursus on 15.2.2016.
 */
public class InjectionHookImpl implements InjectionHook {
	@Override
	public void execute() {
		System.out.println("I am injected object from hook!");
	}
}
