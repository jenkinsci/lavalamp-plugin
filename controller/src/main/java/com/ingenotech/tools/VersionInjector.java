package com.ingenotech.tools;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;


@SupportedAnnotationTypes("com.ingenotech.annotations.Version")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class VersionInjector extends AbstractProcessor 
{
	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			               RoundEnvironment roundEnv) {
		for (TypeElement type : annotations) {
			process(roundEnv, type);
		}
        return true;
	}
	
	private void process(RoundEnvironment env,
			             TypeElement type) {
		
	}
}
