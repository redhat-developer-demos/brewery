project(xmlns:'http://maven.apache.org/POM/4.0.0',
        'xmlns:xsi':"http://www.w3.org/2001/XMLSchema-instance",
        'xsi:schemaLocation':"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd"){
    modelVersion('4.0.0')
    newLine()
    groupId("${groupId}")
    newLine()
    artifactId("${artifactId}")
    newLine()
    version("${version}")
    newLine()
    properties {
        'project.build.sourceEncoding'('UTF-8')
        newLine()
        'maven.compiler.source'('1.8')
        newLine()
        'maven.compiler.target'('1.8')
        newLine()
        if (fabric8) {
            'fabric8.maven.plugin.version'("${f8mpVersion}")
            newLine()
            'fabric8.generator.spring-boot.activeProfiles'('ocp')
        }
        newLine()
        'spring-boot-maven-plugin.version'('1.5.1.RELEASE')
        newLine()
        'spring-cloud.version'("${springCloudVersion}")
        newLine()
        if (springBoot) {
            'spring-boot.version'("${springBootVersion}")
        }
    }
    newLine()
    dependencyManagement {
       dependencies {
            dependency {
                groupId('org.springframework.cloud')
                newLine()
                artifactId('spring-cloud-dependencies')
                newLine()
                version('${spring-cloud.version}')
                newLine()
                type('pom')
                newLine()
                scope('import')
            }
            newLine()
            if(springBoot){
                dependency {
                    groupId('org.springframework.boot')
                    newLine()
                    artifactId('spring-boot-dependencies')
                    newLine()
                    version('${spring-boot.version}')
                    newLine()
                    type('pom')
                    newLine()
                    scope('import')
                }
            }
       }
    }
    newLine()
    yieldUnescaped "${projectDeps}"
    newLine()
    build {
        if(groovy) {
          sourceDirectory('src/main/groovy')
        }
        resources {
            resource {
                directory('src/main/resources')
            }
            newLine()
             if (fabric8) {
                resource {
                    directory('src/main/fabric8')
                    newLine()
                    filtering(true)
                }
             }
        }
        newLine()
        plugins {
            if(fabric8) {
               plugin {
                  groupId('io.fabric8')
                  newLine()
                  artifactId('fabric8-maven-plugin')
                  newLine()
                  version('${fabric8.maven.plugin.version}')
                  newLine()
                  executions {
                      execution {
                          goals {
                             goal('build')
                             newLine()
                             goal('resource')
                          }
                      }
                  }
               }
             newLine()
            }

            if(springBoot) {
               plugin {
                  groupId('org.springframework.boot')
                  newLine()
                  artifactId('spring-boot-maven-plugin')
                  newLine()
                  version('${spring-boot-maven-plugin.version}')
                  newLine()
                  executions {
                      execution {
                          goals {
                             goal('repackage')
                          }
                      }
                  }
               }
               newLine()
            }

            if(groovy) {
                plugin {
                     groupId('org.apache.maven.plugins')
                     newLine()
                     artifactId('maven-antrun-plugin')
                     newLine()
                     version('1.8')
                     newLine()
                     executions {
                         execution {
                             id('compile')
                             phase('compile')
                             configuration {
                                tasks{
                                   mkdir(dir:"src/main/groovy")
                                   taskdef(name: 'groovyc', classname:'org.codehaus.groovy.ant.Groovyc'){
                                     classpath(refid: 'maven.compile.classpath')
                                   }
                                   newLine()
                                   groovyc(destdir: '${project.build.outputDirectory}',
                                           srcdir: '${basedir}/src/main/groovy/',
                                           listfiles: 'true'){
                                      classpath(refid: 'maven.compile.classpath')
                                   }
                                }
                             }
                             newLine()
                             goals {
                                goal('run')
                             }
                         }
                         newLine()
                         execution {
                              id('test-compile')
                              phase('test-compile')
                              configuration {
                                 tasks{
                                    mkdir(dir:"src/test/groovy")
                                    taskdef(name: 'groovyc', classname:'org.codehaus.groovy.ant.Groovyc'){
                                      classpath(refid: 'maven.test.classpath')
                                    }
                                    newLine()
                                    groovyc(destdir: '${project.build.outputDirectory}',
                                            srcdir: '${basedir}/src/test/groovy/',
                                            listfiles: 'true'){
                                       classpath(refid: 'maven.test.classpath')
                                    }
                                 }
                              }
                              newLine()
                              goals {
                                 goal('run')
                              }
                         }

                     }
                  }
            }

        }
    }
}