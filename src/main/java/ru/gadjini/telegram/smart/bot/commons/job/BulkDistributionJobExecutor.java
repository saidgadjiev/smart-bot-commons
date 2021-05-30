package ru.gadjini.telegram.smart.bot.commons.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.Profiles;

@Component
@Profile({Profiles.PROFILE_DEV_PRIMARY, Profiles.PROFILE_PROD_PRIMARY})
public class BulkDistributionJobExecutor {

    private BulkDistributionJob bulkDistributionJob;

    @Autowired
    public BulkDistributionJobExecutor(BulkDistributionJob bulkDistributionJob) {
        this.bulkDistributionJob = bulkDistributionJob;
    }

    @Scheduled(fixedDelay = 1000)
    public void distribute() {
        bulkDistributionJob.distribute();
    }
}
