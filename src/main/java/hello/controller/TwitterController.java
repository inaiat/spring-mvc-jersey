package hello.controller;

import com.hazelcast.core.IList;
import hello.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/")
public class TwitterController {

    @Resource
    @Qualifier(value = "tweetCache")
    private IList<Tweet> tweetCache;

	@Autowired
	private TaskService taskService;

    @RequestMapping(
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUniqueTweet() {
        return ResponseEntity.ok(tweetCache.get(0));
    }

    @RequestMapping(
            value = "random",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getRandomTweet() {
        return random()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
    }

    public Optional<Tweet> random() {
        long count = tweetCache.size();
        if (count == 0) return Optional.empty();
        Random r = new Random();
        long randomIndex = count <= Integer.MAX_VALUE ? r.nextInt((int) count) :
                r.longs(1, 0, count).findFirst().orElseThrow(AssertionError::new);
        return Optional.of(tweetCache.get((int) randomIndex));
    }

	@RequestMapping(
		value = "slow",
		method = RequestMethod.GET,
		produces = MediaType.APPLICATION_JSON_VALUE)
	public DeferredResult<String> slow() throws InterruptedException, ExecutionException {
		DeferredResult<String> deferredResult = new DeferredResult<>();
		CompletableFuture.supplyAsync(taskService::execute)
			.whenCompleteAsync((result, throwable) -> deferredResult.setResult(result));
		return deferredResult;
	}
}
