package hello.controller;

import com.hazelcast.core.IList;
import hello.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
@Path("/")
public class JerseyTwitterController {

	@Autowired
	private TaskService taskService;


	@Resource
    @Qualifier(value = "tweetCache")
    private IList<Tweet> tweetCache;

    @GET
    public Response getUniqueTweet() {
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(tweetCache.get(0)).build();
    }

    @GET
    @Path("random")
    public Response getRandomTweet() {
        return random()
                .map(tweet -> Response.ok().type(MediaType.APPLICATION_JSON).entity(tweet).build())
                .orElse(Response.serverError().build());
    }

	@GET
	@Path("slow")
	public void slow(@Suspended final AsyncResponse asyncResponse) throws InterruptedException, ExecutionException {
		CompletableFuture.supplyAsync(taskService::execute)
			.whenCompleteAsync((result, throwable) -> asyncResponse.resume(Response.ok(result)));
	}

	@GET
	@Path("async")
	public void async(@Suspended final AsyncResponse asyncResponse) {
		CompletableFuture
			.supplyAsync(() -> random()).whenComplete((tweet, throwable) ->  {
				if (tweet.isPresent())
					asyncResponse.resume(Response.ok().type(MediaType.APPLICATION_JSON).entity(tweet).build());
				else
					asyncResponse.resume(Response.serverError().build());
			});

	}

    public Optional<Tweet> random() {
        long count = tweetCache.size();
        if(count==0) return Optional.empty();
        Random r = new Random();
        long randomIndex=count<=Integer.MAX_VALUE? r.nextInt((int)count):
                r.longs(1, 0, count).findFirst().orElseThrow(AssertionError::new);
        return Optional.of(tweetCache.get((int) randomIndex));
    }

}
