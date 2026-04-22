<script>
import { initializeBackendSync } from './store/love'

export default {
  onLaunch() {
    uni.setNavigationBarColor({
      frontColor: '#000000',
      backgroundColor: '#fff6ee'
    })
    initializeBackendSync()
  }
}
</script>

<style>
@import './styles/common.css';
</style>
