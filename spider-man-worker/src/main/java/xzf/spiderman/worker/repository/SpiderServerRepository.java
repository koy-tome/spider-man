package xzf.spiderman.worker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xzf.spiderman.worker.entity.SpiderServer;

public interface SpiderServerRepository extends JpaRepository<SpiderServer, String>
{
}
