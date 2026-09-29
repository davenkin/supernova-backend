package com.company.andy.common.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;

// Class name should follow format: "Migration[3 digits index]_[SimpleDescriptionOfYourMigration]"
@Slf4j
@ChangeUnit(id = "Migration001_Sample", order = "001", author = "andy")
public class Migration001_Sample {

    @Execution
    public void execute(MongoTemplate mongoTemplate) {
        // use mongoTemplate to manipulate the database
        log.debug("Sample change log executed.");
    }

    @RollbackExecution
    public void rollback(MongoTemplate mongoTemplate) {
        // If the migration is only for DB and transaction is enabled,
        // there is no need to rollback as it's already covered by transactions,
        // but you still need this method as Mongock requires at least one method annotated with @RollbackExecution,
        // so just leave the method body empty.
        // But if your migration contains non-DB operations,
        // such as calling remote APIs,
        // or it's only DB operations but the transaction is not enabled,
        // you need to roll back the operations explicitly.
        log.debug("Sample change log rolled back.");
    }
}
