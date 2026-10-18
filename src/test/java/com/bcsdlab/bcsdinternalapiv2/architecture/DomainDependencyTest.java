package com.bcsdlab.bcsdinternalapiv2.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * 최상위 도메인 패키지 사이에 새 순환 의존이 생기지 않게 막는다.
 *
 * <p>이미 있는 순환 중 다음은 허용 예외로 둔다. 예외로 둔 방향의 의존은 검사에서 통째로 빠지므로,
 * 그 의존을 거쳐 생기는 순환(예: member → auth → X → member)도 잡히지 않는다. 예외를 늘리지 않는다.
 * <ul>
 *   <li>auth ↔ member: member가 로그인 응답·비밀번호 재설정 등 auth 기능을 쓴다. member → auth를 무시한다.</li>
 *   <li>track ↔ curriculum: 트랙 상세가 커리큘럼을 함께 내려준다. track → curriculum을 무시한다.</li>
 *   <li>global.config: 보안·MVC 설정이 각 도메인의 컴포넌트를 조립하는 곳이라 검사에서 뺀다.</li>
 * </ul>
 */
@AnalyzeClasses(packages = "com.bcsdlab.bcsdinternalapiv2", importOptions = ImportOption.DoNotIncludeTests.class)
class DomainDependencyTest {

    private static final String ROOT = "com.bcsdlab.bcsdinternalapiv2";

    @ArchTest
    static final ArchRule 도메인_패키지_사이에_순환_의존이_없다 = slices()
            .matching(ROOT + ".(*)..")
            .should().beFreeOfCycles()
            .ignoreDependency(resideInAPackage(ROOT + ".global.config.."), alwaysTrue())
            .ignoreDependency(resideInAPackage(ROOT + ".member.."), resideInAPackage(ROOT + ".auth.."))
            .ignoreDependency(resideInAPackage(ROOT + ".track.."), resideInAPackage(ROOT + ".curriculum.."));
}
